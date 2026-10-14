package com.acme.transport.crewing.qualification.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingQualificationNoticeTest {
    @Test
    fun `starts in created`() {
        val item = newRollingQualificationNotice("code-919", "name-875")
        assertEquals(RollingQualificationNoticeStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newRollingQualificationNotice("code-919", "name-875")
        try {
            item.moveTo(RollingQualificationNoticeStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingQualificationNoticeStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRollingQualificationNotice("code-919", "name-875")
        item.status = RollingQualificationNoticeStatus.RETIRED
        assertFails("move") { item.moveTo(RollingQualificationNoticeStatus.CREATED) }
    }
}
