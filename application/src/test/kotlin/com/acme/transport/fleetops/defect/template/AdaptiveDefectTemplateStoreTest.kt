package com.acme.transport.fleetops.defect.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveDefectTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveDefectTemplateStore()
        val item = newAdaptiveDefectTemplate("code-307", "title-360")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
