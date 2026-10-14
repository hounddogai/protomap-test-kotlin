package com.acme.transport.crewing.qualification.tieredqualificationdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalQualificationNoticeTest {
    @Test
    fun `starts in proposed`() {
        val item = newRegionalQualificationNotice("code-230", "title-182")
        assertEquals(RegionalQualificationNoticeStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRegionalQualificationNotice("code-230", "title-182")
        try {
            item.moveTo(RegionalQualificationNoticeStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalQualificationNoticeStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRegionalQualificationNotice("code-230", "title-182")
        item.status = RegionalQualificationNoticeStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RegionalQualificationNoticeStatus.PROPOSED) }
    }
}
