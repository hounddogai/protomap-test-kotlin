package com.acme.transport.emissions.idling.regionalidlingmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalIdlingExceptionTest {
    @Test
    fun `starts in planned`() {
        val item = newRegionalIdlingException("code-913", "title-399")
        assertEquals(RegionalIdlingExceptionStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newRegionalIdlingException("code-913", "title-399")
        try {
            item.moveTo(RegionalIdlingExceptionStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalIdlingExceptionStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRegionalIdlingException("code-913", "title-399")
        item.status = RegionalIdlingExceptionStatus.CANCELLED
        assertFails("move") { item.moveTo(RegionalIdlingExceptionStatus.PLANNED) }
    }
}
