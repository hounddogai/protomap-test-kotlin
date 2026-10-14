package com.acme.transport.linehaul.tender.tenderbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedTenderWindowTest {
    @Test
    fun `starts in created`() {
        val item = newCappedTenderWindow("code-763", "name-788")
        assertEquals(CappedTenderWindowStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newCappedTenderWindow("code-763", "name-788")
        try {
            item.moveTo(CappedTenderWindowStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedTenderWindowStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCappedTenderWindow("code-763", "name-788")
        item.status = CappedTenderWindowStatus.RETIRED
        assertFails("move") { item.moveTo(CappedTenderWindowStatus.CREATED) }
    }
}
