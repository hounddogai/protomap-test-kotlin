package com.acme.transport.fleetops.inspection.inspectionscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryInspectionMappingTest {
    @Test
    fun `starts in new`() {
        val item = newPrimaryInspectionMapping("code-730", "label-319")
        assertEquals(PrimaryInspectionMappingStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newPrimaryInspectionMapping("code-730", "label-319")
        try {
            item.moveTo(PrimaryInspectionMappingStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryInspectionMappingStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPrimaryInspectionMapping("code-730", "label-319")
        item.status = PrimaryInspectionMappingStatus.FAILED
        assertFails("move") { item.moveTo(PrimaryInspectionMappingStatus.NEW) }
    }
}
