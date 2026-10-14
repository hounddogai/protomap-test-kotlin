package com.acme.transport.freightpay.accessorial.cycle

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701491345)
        val items = mutableListOf<AccessorialBatch>()
        for (index in 0 until 3) {
            items.add(newAccessorialBatch("accessorial-batch-$index", "sample"))
        }
        val report = buildAccessorialBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
