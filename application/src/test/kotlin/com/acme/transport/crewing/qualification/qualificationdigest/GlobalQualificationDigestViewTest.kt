package com.acme.transport.crewing.qualification.qualificationdigest

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalQualificationDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalQualificationDigest("code-546", "name-363")
        val view = toGlobalQualificationDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalQualificationDigestViews(mutableListOf<GlobalQualificationDigest>(item, item)).size, "views")
    }
}
