package com.acme.transport.crewing.qualification.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalQualificationNoticeTest {
    @Test
    fun `starts in idle`() {
        val item = newSeasonalQualificationNotice("code-743", "label-472")
        assertEquals(SeasonalQualificationNoticeStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newSeasonalQualificationNotice("code-743", "label-472")
        try {
            item.moveTo(SeasonalQualificationNoticeStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalQualificationNoticeStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newSeasonalQualificationNotice("code-743", "label-472")
        item.status = SeasonalQualificationNoticeStatus.STOPPED
        assertFails("move") { item.moveTo(SeasonalQualificationNoticeStatus.IDLE) }
    }
}
