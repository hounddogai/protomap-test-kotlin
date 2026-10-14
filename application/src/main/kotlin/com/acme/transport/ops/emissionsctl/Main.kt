package com.acme.transport.ops.emissionsctl

import com.acme.transport.emissions.carbonreport.template.buildCarbonReportCycleReport
import com.acme.transport.emissions.carbonreport.template.newCarbonReportCycle
import com.acme.transport.emissions.carbonreport.template.newCarbonReportCycleWorkflow
import com.acme.transport.emissions.carbonreport.template.newMemoryCarbonReportCycleStore
import com.acme.transport.emissions.clock.system
import com.acme.transport.emissions.emissionfactor.preference.buildEmissionFactorPolicyReport
import com.acme.transport.emissions.emissionfactor.preference.newEmissionFactorPolicy
import com.acme.transport.emissions.emissionfactor.preference.newEmissionFactorPolicyManager
import com.acme.transport.emissions.emissionfactor.preference.newMemoryEmissionFactorPolicyStore
import com.acme.transport.emissions.idling.target.buildIdlingCheckpointReport
import com.acme.transport.emissions.idling.target.newIdlingCheckpoint
import com.acme.transport.emissions.idling.target.newIdlingCheckpointService
import com.acme.transport.emissions.idling.target.newMemoryIdlingCheckpointStore
import com.acme.transport.emissions.routeoffset.rollup.buildRouteOffsetRequestReport
import com.acme.transport.emissions.routeoffset.rollup.newMemoryRouteOffsetRequestStore
import com.acme.transport.emissions.routeoffset.rollup.newRouteOffsetRequest
import com.acme.transport.emissions.routeoffset.rollup.newRouteOffsetRequestDesk
import com.acme.transport.kit.flags.newRegistry
import kotlin.system.exitProcess

fun main() {
    val clock = system()
    val commands = mutableMapOf<String, () -> Boolean>(
        "emission-factor-policy" to fun(): Boolean {
            val store = newMemoryEmissionFactorPolicyStore()
            for (index in 0 until 12) {
                store.save(newEmissionFactorPolicy("emission-factor-policy-$index", "Sample emission factor policy $index"))
            }
            val flow = newEmissionFactorPolicyManager(store, clock, newRegistry())
            try {
                flow.start("emission-factor-policy-0")
            } catch (failure: Exception) {
                return false
            }
            val items = store.list()
            val report = buildEmissionFactorPolicyReport("Emission Factor Policy", items, clock)
            println(report.render())
            return flow.scan((System.currentTimeMillis() / 1000).toInt()) >= 0
        },
        "carbon-report-cycle" to fun(): Boolean {
            val store = newMemoryCarbonReportCycleStore()
            for (index in 0 until 9) {
                store.save(newCarbonReportCycle("carbon-report-cycle-$index", "Sample carbon report cycle $index"))
            }
            val flow = newCarbonReportCycleWorkflow(store, clock)
            try {
                flow.advance("carbon-report-cycle-0")
            } catch (failure: Exception) {
                return false
            }
            val items = store.list()
            val report = buildCarbonReportCycleReport("Carbon Report Cycle", items, clock)
            println(report.render())
            return flow.audit((System.currentTimeMillis() / 1000).toInt()) >= 0
        },
        "route-offset-request" to fun(): Boolean {
            val store = newMemoryRouteOffsetRequestStore()
            for (index in 0 until 4) {
                store.save(newRouteOffsetRequest("route-offset-request-$index", "Sample route offset request $index"))
            }
            val flow = newRouteOffsetRequestDesk(store, clock)
            try {
                flow.start("route-offset-request-0")
            } catch (failure: Exception) {
                return false
            }
            val items = store.list()
            val report = buildRouteOffsetRequestReport("Route Offset Request", items, clock)
            println(report.render())
            return flow.reviewStale((System.currentTimeMillis() / 1000).toInt()) >= 0
        },
        "idling-checkpoint" to fun(): Boolean {
            val store = newMemoryIdlingCheckpointStore()
            for (index in 0 until 7) {
                store.save(newIdlingCheckpoint("idling-checkpoint-$index", "Sample idling checkpoint $index"))
            }
            val flow = newIdlingCheckpointService(store, clock, newRegistry())
            try {
                flow.open("idling-checkpoint-0")
            } catch (failure: Exception) {
                return false
            }
            val items = store.list()
            val report = buildIdlingCheckpointReport("Idling Checkpoint", items, clock)
            println(report.render())
            return flow.audit((System.currentTimeMillis() / 1000).toInt()) >= 0
        },
    )
    var failed = 0
    for ((name, command) in commands.toSortedMap()) {
        if (!command()) {
            println("$name failed")
            failed += 1
        }
    }
    if (failed > 0) {
        exitProcess(1)
    }
}
