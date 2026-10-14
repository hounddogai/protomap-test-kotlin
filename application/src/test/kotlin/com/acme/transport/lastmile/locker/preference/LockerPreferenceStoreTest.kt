package com.acme.transport.lastmile.locker.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LockerPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLockerPreferenceStore()
        val item = newLockerPreference("code-237", "name-341")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
