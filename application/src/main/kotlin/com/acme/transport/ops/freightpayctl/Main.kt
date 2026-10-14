package com.acme.transport.ops.freightpayctl

import com.acme.transport.freightpay.clock.system
import com.acme.transport.freightpay.freightinvoice.manifest.buildFreightInvoiceCheckpointReport
import com.acme.transport.freightpay.freightinvoice.manifest.newFreightInvoiceCheckpoint
import com.acme.transport.freightpay.freightinvoice.manifest.newFreightInvoiceCheckpointCoordinator
import com.acme.transport.freightpay.freightinvoice.manifest.newMemoryFreightInvoiceCheckpointStore
import com.acme.transport.freightpay.fuelsurcharge.request.buildFuelSurchargeVarianceReport
import com.acme.transport.freightpay.fuelsurcharge.request.newFuelSurchargeVariance
import com.acme.transport.freightpay.fuelsurcharge.request.newFuelSurchargeVarianceDesk
import com.acme.transport.freightpay.fuelsurcharge.request.newMemoryFuelSurchargeVarianceStore
import com.acme.transport.freightpay.ratecard.cycle.buildRateCardCycleReport
import com.acme.transport.freightpay.ratecard.cycle.newMemoryRateCardCycleStore
import com.acme.transport.freightpay.ratecard.cycle.newRateCardCycle
import com.acme.transport.freightpay.ratecard.cycle.newRateCardCycleManager
import com.acme.transport.kit.flags.newRegistry
import kotlin.system.exitProcess

fun main() {
    val clock = system()
    val commands = mutableMapOf<String, () -> Boolean>(
        "freight-invoice-checkpoint" to fun(): Boolean {
            val store = newMemoryFreightInvoiceCheckpointStore()
            for (index in 0 until 7) {
                store.save(newFreightInvoiceCheckpoint("freight-invoice-checkpoint-$index", "Sample freight invoice checkpoint $index"))
            }
            val flow = newFreightInvoiceCheckpointCoordinator(store, clock, newRegistry())
            try {
                flow.open("freight-invoice-checkpoint-0")
            } catch (failure: Exception) {
                return false
            }
            val items = store.list()
            val report = buildFreightInvoiceCheckpointReport("Freight Invoice Checkpoint", items, clock)
            println(report.render())
            return flow.scan((System.currentTimeMillis() / 1000).toInt()) >= 0
        },
        "rate-card-cycle" to fun(): Boolean {
            val store = newMemoryRateCardCycleStore()
            for (index in 0 until 12) {
                store.save(newRateCardCycle("rate-card-cycle-$index", "Sample rate card cycle $index"))
            }
            val flow = newRateCardCycleManager(store, clock, newRegistry())
            try {
                flow.start("rate-card-cycle-0")
            } catch (failure: Exception) {
                return false
            }
            val items = store.list()
            val report = buildRateCardCycleReport("Rate Card Cycle", items, clock)
            println(report.render())
            return flow.scan((System.currentTimeMillis() / 1000).toInt()) >= 0
        },
        "fuel-surcharge-variance" to fun(): Boolean {
            val store = newMemoryFuelSurchargeVarianceStore()
            for (index in 0 until 4) {
                store.save(newFuelSurchargeVariance("fuel-surcharge-variance-$index", "Sample fuel surcharge variance $index"))
            }
            val flow = newFuelSurchargeVarianceDesk(store, clock)
            try {
                flow.open("fuel-surcharge-variance-0")
            } catch (failure: Exception) {
                return false
            }
            val items = store.list()
            val report = buildFuelSurchargeVarianceReport("Fuel Surcharge Variance", items, clock)
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
