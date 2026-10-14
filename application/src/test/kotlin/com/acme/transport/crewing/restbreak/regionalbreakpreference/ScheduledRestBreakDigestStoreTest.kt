package com.acme.transport.crewing.restbreak.regionalbreakpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledRestBreakDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledRestBreakDigestStore()
        val item = newScheduledRestBreakDigest("code-199", "title-306")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
