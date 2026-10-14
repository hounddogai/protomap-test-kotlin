package com.acme.transport.lastmile.drop.dropnotice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps rolling drop notices by code. */
interface RollingDropNoticeStore {
    fun find(code: String): RollingDropNotice?
    fun save(item: RollingDropNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingDropNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingDropNoticeStore(
    var items: MutableMap<String, RollingDropNotice>,
    var order: MutableList<String>,
) : RollingDropNoticeStore {
    override fun find(code: String): RollingDropNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingDropNotice) {
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

    override fun list(): MutableList<RollingDropNotice> {
        val out = mutableListOf<RollingDropNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingDropNoticeStore(): RollingDropNoticeStore {
    return MemoryRollingDropNoticeStore(mutableMapOf<String, RollingDropNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingDropNoticeStore(
    var inner: RollingDropNoticeStore,
    var cache: TtlCache<RollingDropNotice>,
    var hits: Int,
    var misses: Int,
) : RollingDropNoticeStore {
    override fun find(code: String): RollingDropNotice? {
        val hit = (cache.get(code) as RollingDropNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingDropNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingDropNotice> = inner.list()
}

fun newCachedRollingDropNoticeStore(inner: RollingDropNoticeStore, ttl: Int, clock: Clock): RollingDropNoticeStore {
    return CachedRollingDropNoticeStore(
        inner = inner,
        cache = newTtlCache<RollingDropNotice>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling drop notices. Empty fields match all. */
data class RollingDropNoticeQuery(
    var label: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingDropNotice): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingDropNotice>): MutableList<RollingDropNotice> {
        val matched = filter(items) { candidate: RollingDropNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingDropNotices(store: RollingDropNoticeStore, query: RollingDropNoticeQuery): MutableList<RollingDropNotice> {
    return query.apply(store.list())
}
