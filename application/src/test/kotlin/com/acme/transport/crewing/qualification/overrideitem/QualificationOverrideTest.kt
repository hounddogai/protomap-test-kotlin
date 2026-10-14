package com.acme.transport.crewing.qualification.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class QualificationOverrideTest {
    @Test
    fun `starts in created`() {
        val item = newQualificationOverride("code-76", "title-188")
        assertEquals(QualificationOverrideStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newQualificationOverride("code-76", "title-188")
        try {
            item.moveTo(QualificationOverrideStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(QualificationOverrideStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newQualificationOverride("code-76", "title-188")
        item.status = QualificationOverrideStatus.RETIRED
        assertFails("move") { item.moveTo(QualificationOverrideStatus.CREATED) }
    }
}
