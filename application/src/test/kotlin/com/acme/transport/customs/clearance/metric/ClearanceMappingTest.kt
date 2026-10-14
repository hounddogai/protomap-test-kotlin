package com.acme.transport.customs.clearance.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ClearanceMappingTest {
    @Test
    fun `starts in created`() {
        val item = newClearanceMapping("code-913", "title-322")
        assertEquals(ClearanceMappingStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newClearanceMapping("code-913", "title-322")
        try {
            item.moveTo(ClearanceMappingStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ClearanceMappingStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newClearanceMapping("code-913", "title-322")
        item.status = ClearanceMappingStatus.RETIRED
        assertFails("move") { item.moveTo(ClearanceMappingStatus.CREATED) }
    }
}
