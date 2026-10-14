package com.acme.transport.linehaul.tender.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalTenderTemplateTest {
    @Test
    fun `starts in new`() {
        val item = newRegionalTenderTemplate("code-654", "title-22")
        assertEquals(RegionalTenderTemplateStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newRegionalTenderTemplate("code-654", "title-22")
        try {
            item.moveTo(RegionalTenderTemplateStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalTenderTemplateStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRegionalTenderTemplate("code-654", "title-22")
        item.status = RegionalTenderTemplateStatus.FAILED
        assertFails("move") { item.moveTo(RegionalTenderTemplateStatus.NEW) }
    }
}
