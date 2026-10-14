package com.acme.transport.safety.coaching.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryCoachingMappingTest {
    @Test
    fun `starts in open`() {
        val item = newPrimaryCoachingMapping("code-31", "title-516")
        assertEquals(PrimaryCoachingMappingStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newPrimaryCoachingMapping("code-31", "title-516")
        try {
            item.moveTo(PrimaryCoachingMappingStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryCoachingMappingStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPrimaryCoachingMapping("code-31", "title-516")
        item.status = PrimaryCoachingMappingStatus.ARCHIVED
        assertFails("move") { item.moveTo(PrimaryCoachingMappingStatus.OPEN) }
    }
}
