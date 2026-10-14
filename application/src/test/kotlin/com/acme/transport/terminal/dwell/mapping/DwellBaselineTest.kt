package com.acme.transport.terminal.dwell.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DwellBaselineTest {
    @Test
    fun `starts in proposed`() {
        val item = newDwellBaseline("code-736", "title-107")
        assertEquals(DwellBaselineStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newDwellBaseline("code-736", "title-107")
        try {
            item.moveTo(DwellBaselineStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DwellBaselineStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDwellBaseline("code-736", "title-107")
        item.status = DwellBaselineStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DwellBaselineStatus.PROPOSED) }
    }
}
