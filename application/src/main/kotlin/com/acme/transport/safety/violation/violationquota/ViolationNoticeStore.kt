package com.acme.transport.safety.violation.violationquota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation notices by code. */
interface ViolationNoticeStore {
    fun find(code: String): ViolationNotice?
    fun save(item: ViolationNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationNoticeStore(
    var items: MutableMap<String, ViolationNotice>,
    var order: MutableList<String>,
) : ViolationNoticeStore {
    override fun find(code: String): ViolationNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationNotice) {
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

    override fun list(): MutableList<ViolationNotice> {
        val out = mutableListOf<ViolationNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationNoticeStore(): ViolationNoticeStore {
    return MemoryViolationNoticeStore(mutableMapOf<String, ViolationNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationNoticeStore(
    var inner: ViolationNoticeStore,
    var cache: TtlCache<ViolationNotice>,
    var hits: Int,
    var misses: Int,
) : ViolationNoticeStore {
    override fun find(code: String): ViolationNotice? {
        val hit = (cache.get(code) as ViolationNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationNotice> = inner.list()
}

fun newCachedViolationNoticeStore(inner: ViolationNoticeStore, ttl: Int, clock: Clock): ViolationNoticeStore {
    return CachedViolationNoticeStore(
        inner = inner,
        cache = newTtlCache<ViolationNotice>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation notices. Empty fields match all. */
data class ViolationNoticeQuery(
    var unit: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationNotice): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ViolationNotice>): MutableList<ViolationNotice> {
        val matched = filter(items) { candidate: ViolationNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationNotices(store: ViolationNoticeStore, query: ViolationNoticeQuery): MutableList<ViolationNotice> {
    return query.apply(store.list())
}
