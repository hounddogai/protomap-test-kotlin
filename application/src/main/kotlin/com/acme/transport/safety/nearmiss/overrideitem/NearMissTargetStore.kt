package com.acme.transport.safety.nearmiss.overrideitem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss targets by code. */
interface NearMissTargetStore {
    fun find(code: String): NearMissTarget?
    fun save(item: NearMissTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissTargetStore(
    var items: MutableMap<String, NearMissTarget>,
    var order: MutableList<String>,
) : NearMissTargetStore {
    override fun find(code: String): NearMissTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissTarget) {
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

    override fun list(): MutableList<NearMissTarget> {
        val out = mutableListOf<NearMissTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissTargetStore(): NearMissTargetStore {
    return MemoryNearMissTargetStore(mutableMapOf<String, NearMissTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissTargetStore(
    var inner: NearMissTargetStore,
    var cache: TtlCache<NearMissTarget>,
    var hits: Int,
    var misses: Int,
) : NearMissTargetStore {
    override fun find(code: String): NearMissTarget? {
        val hit = (cache.get(code) as NearMissTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissTarget> = inner.list()
}

fun newCachedNearMissTargetStore(inner: NearMissTargetStore, ttl: Int, clock: Clock): NearMissTargetStore {
    return CachedNearMissTargetStore(
        inner = inner,
        cache = newTtlCache<NearMissTarget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss targets. Empty fields match all. */
data class NearMissTargetQuery(
    var lane: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissTarget): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NearMissTarget>): MutableList<NearMissTarget> {
        val matched = filter(items) { candidate: NearMissTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissTargets(store: NearMissTargetStore, query: NearMissTargetQuery): MutableList<NearMissTarget> {
    return query.apply(store.list())
}
