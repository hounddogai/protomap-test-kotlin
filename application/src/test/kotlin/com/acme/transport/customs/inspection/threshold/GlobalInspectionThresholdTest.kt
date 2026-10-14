package com.acme.transport.customs.inspection.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalInspectionThresholdTest {
    @Test
    fun `starts in requested`() {
        val item = newGlobalInspectionThreshold("code-729", "title-262")
        assertEquals(GlobalInspectionThresholdStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newGlobalInspectionThreshold("code-729", "title-262")
        try {
            item.moveTo(GlobalInspectionThresholdStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalInspectionThresholdStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newGlobalInspectionThreshold("code-729", "title-262")
        item.status = GlobalInspectionThresholdStatus.EXPIRED
        assertFails("move") { item.moveTo(GlobalInspectionThresholdStatus.REQUESTED) }
    }
}
