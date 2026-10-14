package com.acme.transport.ops.safetyctl

import com.acme.transport.safety.clock.system
import com.acme.transport.safety.incident.baseline.buildIncidentBaselineReport
import com.acme.transport.safety.incident.baseline.newIncidentBaseline
import com.acme.transport.safety.incident.baseline.newIncidentBaselineService
import com.acme.transport.safety.incident.baseline.newMemoryIncidentBaselineStore
import kotlin.system.exitProcess

fun main() {
    val clock = system()
    val commands = mutableMapOf<String, () -> Boolean>(
        "incident-baseline" to fun(): Boolean {
            val store = newMemoryIncidentBaselineStore()
            for (index in 0 until 3) {
                store.save(newIncidentBaseline("incident-baseline-$index", "Sample incident baseline $index"))
            }
            val flow = newIncidentBaselineService(store, clock)
            try {
                flow.submit("incident-baseline-0")
            } catch (failure: Exception) {
                return false
            }
            val items = store.list()
            val report = buildIncidentBaselineReport("Incident Baseline", items, clock)
            println(report.render())
            return flow.scan((System.currentTimeMillis() / 1000).toInt()) >= 0
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
