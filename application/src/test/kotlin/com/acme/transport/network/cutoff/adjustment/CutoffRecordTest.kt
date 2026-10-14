package com.acme.transport.network.cutoff.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CutoffRecordTest {
    @Test
    fun `starts in created`() {
        val item = newCutoffRecord("code-993", "label-452")
        assertEquals(CutoffRecordStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newCutoffRecord("code-993", "label-452")
        try {
            item.moveTo(CutoffRecordStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CutoffRecordStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCutoffRecord("code-993", "label-452")
        item.status = CutoffRecordStatus.RETIRED
        assertFails("move") { item.moveTo(CutoffRecordStatus.CREATED) }
    }
}
