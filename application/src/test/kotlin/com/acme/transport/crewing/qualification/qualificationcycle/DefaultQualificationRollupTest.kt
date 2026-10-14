package com.acme.transport.crewing.qualification.qualificationcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultQualificationRollupTest {
    @Test
    fun `starts in created`() {
        val item = newDefaultQualificationRollup("code-281", "label-746")
        assertEquals(DefaultQualificationRollupStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newDefaultQualificationRollup("code-281", "label-746")
        try {
            item.moveTo(DefaultQualificationRollupStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultQualificationRollupStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDefaultQualificationRollup("code-281", "label-746")
        item.status = DefaultQualificationRollupStatus.RETIRED
        assertFails("move") { item.moveTo(DefaultQualificationRollupStatus.CREATED) }
    }
}
