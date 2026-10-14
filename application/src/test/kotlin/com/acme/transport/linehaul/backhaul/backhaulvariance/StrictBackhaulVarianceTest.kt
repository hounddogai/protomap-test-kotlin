package com.acme.transport.linehaul.backhaul.backhaulvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictBackhaulVarianceTest {
    @Test
    fun `starts in idle`() {
        val item = newStrictBackhaulVariance("code-19", "name-745")
        assertEquals(StrictBackhaulVarianceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newStrictBackhaulVariance("code-19", "name-745")
        try {
            item.moveTo(StrictBackhaulVarianceStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictBackhaulVarianceStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newStrictBackhaulVariance("code-19", "name-745")
        item.status = StrictBackhaulVarianceStatus.STOPPED
        assertFails("move") { item.moveTo(StrictBackhaulVarianceStatus.IDLE) }
    }
}
