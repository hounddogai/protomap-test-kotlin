package com.acme.transport.network.node.schedule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class NodeReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNodeReviewEngine(newRegistry())
        val item = newNodeReview("code-918", "label-141")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
