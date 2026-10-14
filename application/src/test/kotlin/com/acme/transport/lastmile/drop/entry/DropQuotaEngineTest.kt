package com.acme.transport.lastmile.drop.entry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DropQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDropQuotaEngine(newRegistry())
        val item = newDropQuota("code-554", "title-60")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
