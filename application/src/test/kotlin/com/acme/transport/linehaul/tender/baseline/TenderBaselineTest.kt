package com.acme.transport.linehaul.tender.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TenderBaselineTest {
    @Test
    fun `starts in idle`() {
        val item = newTenderBaseline("code-797", "label-857")
        assertEquals(TenderBaselineStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newTenderBaseline("code-797", "label-857")
        try {
            item.moveTo(TenderBaselineStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TenderBaselineStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTenderBaseline("code-797", "label-857")
        item.status = TenderBaselineStatus.STOPPED
        assertFails("move") { item.moveTo(TenderBaselineStatus.IDLE) }
    }
}
