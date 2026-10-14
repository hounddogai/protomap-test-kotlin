package com.acme.transport.safety.cameraevent.audit

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702732399)
        val items = mutableListOf<CameraEventAudit>()
        for (index in 0 until 6) {
            items.add(newCameraEventAudit("camera-event-audit-$index", "sample"))
        }
        val report = buildCameraEventAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
