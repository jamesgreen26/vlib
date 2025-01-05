package io.github.techtastic.vs_addon_template.fabric

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint

/**
 * For now, just using this class as an abusive early entrypoint to run the updater
 */
object ValkyrienPreLaunch: PreLaunchEntrypoint {
    override fun onPreLaunch() {}
}
