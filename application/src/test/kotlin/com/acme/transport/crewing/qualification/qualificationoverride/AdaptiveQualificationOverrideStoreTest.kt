package com.acme.transport.crewing.qualification.qualificationoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveQualificationOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveQualificationOverrideStore()
        val item = newAdaptiveQualificationOverride("code-230", "title-303")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
