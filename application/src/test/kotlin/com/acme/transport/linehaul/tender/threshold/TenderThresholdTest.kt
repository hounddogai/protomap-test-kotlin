package com.acme.transport.linehaul.tender.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TenderThresholdTest {
    @Test
    fun `starts in created`() {
        val item = newTenderThreshold("code-796", "name-796")
        assertEquals(TenderThresholdStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newTenderThreshold("code-796", "name-796")
        try {
            item.moveTo(TenderThresholdStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TenderThresholdStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTenderThreshold("code-796", "name-796")
        item.status = TenderThresholdStatus.RETIRED
        assertFails("move") { item.moveTo(TenderThresholdStatus.CREATED) }
    }
}
