package com.acme.transport.lastmile.locker.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyLockerTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyLockerTemplateStore()
        val item = newLegacyLockerTemplate("code-583", "title-56")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
