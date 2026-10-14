package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedBackhaulVarianceTest {
    @Test
    fun `starts in idle`() {
        val item = newCappedBackhaulVariance("code-168", "title-53")
        assertEquals(CappedBackhaulVarianceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newCappedBackhaulVariance("code-168", "title-53")
        try {
            item.moveTo(CappedBackhaulVarianceStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedBackhaulVarianceStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCappedBackhaulVariance("code-168", "title-53")
        item.status = CappedBackhaulVarianceStatus.STOPPED
        assertFails("move") { item.moveTo(CappedBackhaulVarianceStatus.IDLE) }
    }
}
