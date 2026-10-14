package com.acme.transport.ops.networkctl

import com.acme.transport.network.clock.system
import com.acme.transport.network.node.plan.buildNodePlanReport
import com.acme.transport.network.node.plan.newMemoryNodePlanStore
import com.acme.transport.network.node.plan.newNodePlan
import com.acme.transport.network.node.plan.newNodePlanCoordinator
import kotlin.system.exitProcess

fun main() {
    val clock = system()
    val commands = mutableMapOf<String, () -> Boolean>(
        "node-plan" to fun(): Boolean {
            val store = newMemoryNodePlanStore()
            for (index in 0 until 11) {
                store.save(newNodePlan("node-plan-$index", "Sample node plan $index"))
            }
            val flow = newNodePlanCoordinator(store, clock)
            try {
                flow.open("node-plan-0")
            } catch (failure: Exception) {
                return false
            }
            val items = store.list()
            val report = buildNodePlanReport("Node Plan", items, clock)
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
