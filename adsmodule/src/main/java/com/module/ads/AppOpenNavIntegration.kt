package com.module.ads

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDestination

private const val INVALID_NAV_DEST = -1

/**
 * Integration helpers for **Navigation Component** + **MVVM** (single-activity, `NavHostFragment`).
 *
 * Typical setup:
 * 1. In [androidx.fragment.app.FragmentActivity] `onCreate`, call
 *    [NavController.createAppOpenDestinationGuard] with your splash / consent destination ids so app
 *    open does not cover those screens (optional but recommended).
 * 2. From your splash [Fragment] (or from a [androidx.lifecycle.ViewModel] via a one-shot event that
 *    invokes the Fragment), call your `Application.initAppOpenAfterConsent(adUnitId, requireActivity())`
 *    after UMP consent succeeds — [requireActivity] is the **seed** activity the library needs.
 *
 * Keep a reference to [AppOpenNavDestinationGuard] for the activity lifecycle and call [AppOpenNavDestinationGuard.dispose]
 * in `onDestroy` if you need to clear the predicate (optional).
 */

/**
 * Blocks app-open **display** while the nav graph’s current destination id is one of [blockedDestinationIds].
 * Loading still runs; when the user navigates to a non-blocked destination,
 * [AppOpenAdManager.requestShowAfterNavigationOrDestinationChange] is invoked automatically
 * (the host Activity does not receive another `onResume` when only the NavController destination changes).
 *
 * Install from [androidx.fragment.app.FragmentActivity] **before** or when the first app-open load starts:
 * ```
 * val navController = findNavController(R.id.nav_host_fragment)
 * appOpenNavGuard = navController.createAppOpenDestinationGuard(
 *     R.id.splashFragment,
 *     R.id.consentFragment,
 * )
 * ```
 */
class AppOpenNavDestinationGuard(
    private val navController: NavController,
    private val blockedDestinationIds: Set<Int>,
) : NavController.OnDestinationChangedListener {

    private var currentDestinationId: Int =
        navController.currentDestination?.id ?: INVALID_NAV_DEST

    init {
        navController.addOnDestinationChangedListener(this)
        AppOpenAdManager.setActivityPredicate {
            allowShowForCurrentNavDestination()
        }
    }

    private fun allowShowForCurrentNavDestination(): Boolean {
        val id = currentDestinationId
        if (id == INVALID_NAV_DEST) return false
        return blockedDestinationIds.none { it == id }
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?,
    ) {
        currentDestinationId = destination.id
        if (allowShowForCurrentNavDestination()) {
            AppOpenAdManager.requestShowAfterNavigationOrDestinationChange()
        }
    }

    fun dispose() {
        navController.removeOnDestinationChangedListener(this)
        AppOpenAdManager.setActivityPredicate(null)
    }
}

/**
 * @param blockedDestinationIds Destination **android:id** values (e.g. `R.id.splashFragment`) where app open must not show.
 */
fun NavController.createAppOpenDestinationGuard(
    vararg blockedDestinationIds: Int,
): AppOpenNavDestinationGuard {
    return AppOpenNavDestinationGuard(this, blockedDestinationIds.toSet())
}

/**
 * Use as the second argument to `Application.initAppOpenAfterConsent(adUnitId, seedActivity)` when
 * consent completes inside a `Fragment` (including Nav host children).
 */
fun Fragment.requireActivityForAppOpen(): Activity = requireActivity()
