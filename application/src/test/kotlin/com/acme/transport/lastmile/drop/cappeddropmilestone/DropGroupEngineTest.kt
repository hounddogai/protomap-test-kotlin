package com.acme.transport.lastmile.drop.cappeddropmilestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DropGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDropGroupEngine(newRegistry())
        val item = newDropGroup("code-168", "name-996")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
