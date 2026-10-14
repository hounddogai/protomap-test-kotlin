package com.acme.transport.customs.hold.holdsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictHoldMappingTest {
    @Test
    fun `starts in open`() {
        val item = newStrictHoldMapping("code-219", "title-894")
        assertEquals(StrictHoldMappingStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newStrictHoldMapping("code-219", "title-894")
        try {
            item.moveTo(StrictHoldMappingStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictHoldMappingStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newStrictHoldMapping("code-219", "title-894")
        item.status = StrictHoldMappingStatus.ARCHIVED
        assertFails("move") { item.moveTo(StrictHoldMappingStatus.OPEN) }
    }
}
