package com.acme.transport.ops.fleetopsctl

import com.acme.transport.fleetops.clock.system
import com.acme.transport.fleetops.serviceplan.request.buildServicePlanRequestReport
import com.acme.transport.fleetops.serviceplan.request.newMemoryServicePlanRequestStore
import com.acme.transport.fleetops.serviceplan.request.newServicePlanRequest
import com.acme.transport.fleetops.serviceplan.request.newServicePlanRequestService
import kotlin.system.exitProcess

fun main() {
    val clock = system()
    val commands = mutableMapOf<String, () -> Boolean>(
        "service-plan-request" to fun(): Boolean {
            val store = newMemoryServicePlanRequestStore()
            for (index in 0 until 11) {
                store.save(newServicePlanRequest("service-plan-request-$index", "Sample service plan request $index"))
            }
            val flow = newServicePlanRequestService(store, clock)
            try {
                flow.start("service-plan-request-0")
            } catch (failure: Exception) {
                return false
            }
            val items = store.list()
            val report = buildServicePlanRequestReport("Service Plan Request", items, clock)
            println(report.render())
            return flow.sweep((System.currentTimeMillis() / 1000).toInt()) >= 0
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
