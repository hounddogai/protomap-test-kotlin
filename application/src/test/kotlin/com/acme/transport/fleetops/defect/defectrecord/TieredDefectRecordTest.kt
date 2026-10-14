package com.acme.transport.fleetops.defect.defectrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredDefectRecordTest {
    @Test
    fun `starts in created`() {
        val item = newTieredDefectRecord("code-326", "title-202")
        assertEquals(TieredDefectRecordStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newTieredDefectRecord("code-326", "title-202")
        try {
            item.moveTo(TieredDefectRecordStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredDefectRecordStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTieredDefectRecord("code-326", "title-202")
        item.status = TieredDefectRecordStatus.RETIRED
        assertFails("move") { item.moveTo(TieredDefectRecordStatus.CREATED) }
    }
}
