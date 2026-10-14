package com.acme.transport.ops.linehaulctl

import com.acme.transport.linehaul.clock.system
import com.acme.transport.linehaul.tender.profile.buildTenderQuotaReport
import com.acme.transport.linehaul.tender.profile.newMemoryTenderQuotaStore
import com.acme.transport.linehaul.tender.profile.newTenderQuota
import com.acme.transport.linehaul.tender.profile.newTenderQuotaManager
import com.acme.transport.linehaul.trailer.snapshot.buildTrailerProposalReport
import com.acme.transport.linehaul.trailer.snapshot.newMemoryTrailerProposalStore
import com.acme.transport.linehaul.trailer.snapshot.newTrailerProposal
import com.acme.transport.linehaul.trailer.snapshot.newTrailerProposalManager
import kotlin.system.exitProcess

fun main() {
    val clock = system()
    val commands = mutableMapOf<String, () -> Boolean>(
        "trailer-proposal" to fun(): Boolean {
            val store = newMemoryTrailerProposalStore()
            for (index in 0 until 4) {
                store.save(newTrailerProposal("trailer-proposal-$index", "Sample trailer proposal $index"))
            }
            val flow = newTrailerProposalManager(store, clock)
            try {
                flow.open("trailer-proposal-0")
            } catch (failure: Exception) {
                return false
            }
            val items = store.list()
            val report = buildTrailerProposalReport("Trailer Proposal", items, clock)
            println(report.render())
            return flow.audit((System.currentTimeMillis() / 1000).toInt()) >= 0
        },
        "tender-quota" to fun(): Boolean {
            val store = newMemoryTenderQuotaStore()
            for (index in 0 until 4) {
                store.save(newTenderQuota("tender-quota-$index", "Sample tender quota $index"))
            }
            val flow = newTenderQuotaManager(store, clock)
            try {
                flow.start("tender-quota-0")
            } catch (failure: Exception) {
                return false
            }
            val items = store.list()
            val report = buildTenderQuotaReport("Tender Quota", items, clock)
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
