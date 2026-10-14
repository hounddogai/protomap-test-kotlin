package com.acme.transport.safety.cameraevent.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps lenient camera event policies by code. */
interface LenientCameraEventPolicyStore {
    fun find(code: String): LenientCameraEventPolicy?
    fun save(item: LenientCameraEventPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientCameraEventPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientCameraEventPolicyStore(
    var items: MutableMap<String, LenientCameraEventPolicy>,
    var order: MutableList<String>,
) : LenientCameraEventPolicyStore {
    override fun find(code: String): LenientCameraEventPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientCameraEventPolicy) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<LenientCameraEventPolicy> {
        val out = mutableListOf<LenientCameraEventPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientCameraEventPolicyStore(): LenientCameraEventPolicyStore {
    return MemoryLenientCameraEventPolicyStore(
        mutableMapOf<String, LenientCameraEventPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientCameraEventPolicyStore(
    var inner: LenientCameraEventPolicyStore,
    var cache: TtlCache<LenientCameraEventPolicy>,
    var hits: Int,
    var misses: Int,
) : LenientCameraEventPolicyStore {
    override fun find(code: String): LenientCameraEventPolicy? {
        val hit = (cache.get(code) as LenientCameraEventPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientCameraEventPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientCameraEventPolicy> = inner.list()
}

fun newCachedLenientCameraEventPolicyStore(inner: LenientCameraEventPolicyStore, ttl: Int, clock: Clock): LenientCameraEventPolicyStore {
    return CachedLenientCameraEventPolicyStore(
        inner = inner,
        cache = newTtlCache<LenientCameraEventPolicy>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient camera event policies. Empty fields match all. */
data class LenientCameraEventPolicyQuery(
    var channel: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientCameraEventPolicy): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientCameraEventPolicy>): MutableList<LenientCameraEventPolicy> {
        val matched = filter(items) { candidate: LenientCameraEventPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientCameraEventPolicies(store: LenientCameraEventPolicyStore, query: LenientCameraEventPolicyQuery): MutableList<LenientCameraEventPolicy> {
    return query.apply(store.list())
}
