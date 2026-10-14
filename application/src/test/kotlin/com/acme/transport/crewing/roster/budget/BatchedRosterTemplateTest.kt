package com.acme.transport.crewing.roster.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedRosterTemplateTest {
    @Test
    fun `starts in created`() {
        val item = newBatchedRosterTemplate("code-69", "name-840")
        assertEquals(BatchedRosterTemplateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newBatchedRosterTemplate("code-69", "name-840")
        try {
            item.moveTo(BatchedRosterTemplateStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedRosterTemplateStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newBatchedRosterTemplate("code-69", "name-840")
        item.status = BatchedRosterTemplateStatus.RETIRED
        assertFails("move") { item.moveTo(BatchedRosterTemplateStatus.CREATED) }
    }
}
