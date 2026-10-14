package com.acme.transport.ops.crewingctl

import com.acme.transport.crewing.clock.system
import com.acme.transport.crewing.dutyperiod.notice.buildDutyPeriodNoticeReport
import com.acme.transport.crewing.dutyperiod.notice.newDutyPeriodNotice
import com.acme.transport.crewing.dutyperiod.notice.newDutyPeriodNoticeManager
import com.acme.transport.crewing.dutyperiod.notice.newMemoryDutyPeriodNoticeStore
import com.acme.transport.crewing.qualification.assignment.buildQualificationAssignmentReport
import com.acme.transport.crewing.qualification.assignment.newMemoryQualificationAssignmentStore
import com.acme.transport.crewing.qualification.assignment.newQualificationAssignment
import com.acme.transport.crewing.qualification.assignment.newQualificationAssignmentManager
import com.acme.transport.crewing.restbreak.profile.buildRestBreakProfileReport
import com.acme.transport.crewing.restbreak.profile.newMemoryRestBreakProfileStore
import com.acme.transport.crewing.restbreak.profile.newRestBreakProfile
import com.acme.transport.crewing.restbreak.profile.newRestBreakProfileManager
import com.acme.transport.kit.flags.newRegistry
import kotlin.system.exitProcess

fun main() {
    val clock = system()
    val commands = mutableMapOf<String, () -> Boolean>(
        "duty-period-notice" to fun(): Boolean {
            val store = newMemoryDutyPeriodNoticeStore()
            for (index in 0 until 11) {
                store.save(newDutyPeriodNotice("duty-period-notice-$index", "Sample duty period notice $index"))
            }
            val flow = newDutyPeriodNoticeManager(store, clock)
            try {
                flow.advance("duty-period-notice-0")
            } catch (failure: Exception) {
                return false
            }
            val items = store.list()
            val report = buildDutyPeriodNoticeReport("Duty Period Notice", items, clock)
            println(report.render())
            return flow.audit((System.currentTimeMillis() / 1000).toInt()) >= 0
        },
        "rest-break-profile" to fun(): Boolean {
            val store = newMemoryRestBreakProfileStore()
            for (index in 0 until 9) {
                store.save(newRestBreakProfile("rest-break-profile-$index", "Sample rest break profile $index"))
            }
            val flow = newRestBreakProfileManager(store, clock)
            try {
                flow.open("rest-break-profile-0")
            } catch (failure: Exception) {
                return false
            }
            val items = store.list()
            val report = buildRestBreakProfileReport("Rest Break Profile", items, clock)
            println(report.render())
            return flow.reviewStale((System.currentTimeMillis() / 1000).toInt()) >= 0
        },
        "qualification-assignment" to fun(): Boolean {
            val store = newMemoryQualificationAssignmentStore()
            for (index in 0 until 5) {
                store.save(newQualificationAssignment("qualification-assignment-$index", "Sample qualification assignment $index"))
            }
            val flow = newQualificationAssignmentManager(store, clock, newRegistry())
            try {
                flow.start("qualification-assignment-0")
            } catch (failure: Exception) {
                return false
            }
            val items = store.list()
            val report = buildQualificationAssignmentReport("Qualification Assignment", items, clock)
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
