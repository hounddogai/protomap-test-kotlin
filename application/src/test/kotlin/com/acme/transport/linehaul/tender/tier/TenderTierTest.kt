package com.acme.transport.linehaul.tender.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TenderTierTest {
    @Test
    fun `starts in new`() {
        val item = newTenderTier("code-127", "name-847")
        assertEquals(TenderTierStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newTenderTier("code-127", "name-847")
        try {
            item.moveTo(TenderTierStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TenderTierStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTenderTier("code-127", "name-847")
        item.status = TenderTierStatus.FAILED
        assertFails("move") { item.moveTo(TenderTierStatus.NEW) }
    }
}
