package com.acme.transport.crewing.restbreak.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RestBreakTemplateTest {
    @Test
    fun `starts in created`() {
        val item = newRestBreakTemplate("code-32", "name-652")
        assertEquals(RestBreakTemplateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRestBreakTemplate("code-32", "name-652")
        try {
            item.moveTo(RestBreakTemplateStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RestBreakTemplateStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRestBreakTemplate("code-32", "name-652")
        item.status = RestBreakTemplateStatus.RETIRED
        assertFails("move") { item.moveTo(RestBreakTemplateStatus.CREATED) }
    }
}
