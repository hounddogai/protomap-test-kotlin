package com.acme.transport.ops.terminalctl

import com.acme.transport.kit.flags.newRegistry
import com.acme.transport.terminal.clock.system
import com.acme.transport.terminal.dockdoor.ledger.buildDockDoorLedgerReport
import com.acme.transport.terminal.dockdoor.ledger.newDockDoorLedger
import com.acme.transport.terminal.dockdoor.ledger.newDockDoorLedgerCoordinator
import com.acme.transport.terminal.dockdoor.ledger.newMemoryDockDoorLedgerStore
import com.acme.transport.terminal.sortation.claim.buildSortationSnapshotReport
import com.acme.transport.terminal.sortation.claim.newMemorySortationSnapshotStore
import com.acme.transport.terminal.sortation.claim.newSortationSnapshot
import com.acme.transport.terminal.sortation.claim.newSortationSnapshotCoordinator
import kotlin.system.exitProcess

fun main() {
    val clock = system()
    val commands = mutableMapOf<String, () -> Boolean>(
        "dock-door-ledger" to fun(): Boolean {
            val store = newMemoryDockDoorLedgerStore()
            for (index in 0 until 3) {
                store.save(newDockDoorLedger("dock-door-ledger-$index", "Sample dock door ledger $index"))
            }
            val flow = newDockDoorLedgerCoordinator(store, clock)
            try {
                flow.submit("dock-door-ledger-0")
            } catch (failure: Exception) {
                return false
            }
            val items = store.list()
            val report = buildDockDoorLedgerReport("Dock Door Ledger", items, clock)
            println(report.render())
            return flow.audit((System.currentTimeMillis() / 1000).toInt()) >= 0
        },
        "sortation-snapshot" to fun(): Boolean {
            val store = newMemorySortationSnapshotStore()
            for (index in 0 until 7) {
                store.save(newSortationSnapshot("sortation-snapshot-$index", "Sample sortation snapshot $index"))
            }
            val flow = newSortationSnapshotCoordinator(store, clock, newRegistry())
            try {
                flow.advance("sortation-snapshot-0")
            } catch (failure: Exception) {
                return false
            }
            val items = store.list()
            val report = buildSortationSnapshotReport("Sortation Snapshot", items, clock)
            println(report.render())
            return flow.reviewStale((System.currentTimeMillis() / 1000).toInt()) >= 0
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
