package com.acme.transport.linehaul.tender.tenderbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TenderWindowTest {
    @Test
    fun `starts in open`() {
        val item = newTenderWindow("code-602", "name-744")
        assertEquals(TenderWindowStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newTenderWindow("code-602", "name-744")
        try {
            item.moveTo(TenderWindowStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TenderWindowStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTenderWindow("code-602", "name-744")
        item.status = TenderWindowStatus.ARCHIVED
        assertFails("move") { item.moveTo(TenderWindowStatus.OPEN) }
    }
}
