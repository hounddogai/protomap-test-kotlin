package com.acme.transport.crewing.qualification.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalQualificationWindowTest {
    @Test
    fun `starts in proposed`() {
        val item = newRegionalQualificationWindow("code-303", "name-703")
        assertEquals(RegionalQualificationWindowStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newRegionalQualificationWindow("code-303", "name-703")
        try {
            item.moveTo(RegionalQualificationWindowStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalQualificationWindowStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRegionalQualificationWindow("code-303", "name-703")
        item.status = RegionalQualificationWindowStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RegionalQualificationWindowStatus.PROPOSED) }
    }
}
