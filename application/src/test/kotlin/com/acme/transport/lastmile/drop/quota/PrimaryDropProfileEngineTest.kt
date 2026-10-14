package com.acme.transport.lastmile.drop.quota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryDropProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryDropProfileEngine(newRegistry())
        val item = newPrimaryDropProfile("code-947", "name-10")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
