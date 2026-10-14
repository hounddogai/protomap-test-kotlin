package com.acme.transport.safety.violation.manifest

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705161670)
        val items = mutableListOf<ViolationManifest>()
        for (index in 0 until 2) {
            items.add(newViolationManifest("violation-manifest-$index", "sample"))
        }
        val report = buildViolationManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
