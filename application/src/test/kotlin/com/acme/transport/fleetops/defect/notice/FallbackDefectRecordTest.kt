package com.acme.transport.fleetops.defect.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackDefectRecordTest {
    @Test
    fun `starts in created`() {
        val item = newFallbackDefectRecord("code-259", "label-905")
        assertEquals(FallbackDefectRecordStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newFallbackDefectRecord("code-259", "label-905")
        try {
            item.moveTo(FallbackDefectRecordStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackDefectRecordStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFallbackDefectRecord("code-259", "label-905")
        item.status = FallbackDefectRecordStatus.RETIRED
        assertFails("move") { item.moveTo(FallbackDefectRecordStatus.CREATED) }
    }
}
