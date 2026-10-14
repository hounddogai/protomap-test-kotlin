package com.acme.transport.linehaul.laneplan.budget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan notices by code. */
interface LanePlanNoticeStore {
    fun find(code: String): LanePlanNotice?
    fun save(item: LanePlanNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanNoticeStore(
    var items: MutableMap<String, LanePlanNotice>,
    var order: MutableList<String>,
) : LanePlanNoticeStore {
    override fun find(code: String): LanePlanNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanNotice) {
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

    override fun list(): MutableList<LanePlanNotice> {
        val out = mutableListOf<LanePlanNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanNoticeStore(): LanePlanNoticeStore {
    return MemoryLanePlanNoticeStore(mutableMapOf<String, LanePlanNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanNoticeStore(
    var inner: LanePlanNoticeStore,
    var cache: TtlCache<LanePlanNotice>,
    var hits: Int,
    var misses: Int,
) : LanePlanNoticeStore {
    override fun find(code: String): LanePlanNotice? {
        val hit = (cache.get(code) as LanePlanNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanNotice> = inner.list()
}

fun newCachedLanePlanNoticeStore(inner: LanePlanNoticeStore, ttl: Int, clock: Clock): LanePlanNoticeStore {
    return CachedLanePlanNoticeStore(
        inner = inner,
        cache = newTtlCache<LanePlanNotice>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan notices. Empty fields match all. */
data class LanePlanNoticeQuery(
    var name: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanNotice): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LanePlanNotice>): MutableList<LanePlanNotice> {
        val matched = filter(items) { candidate: LanePlanNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanNotices(store: LanePlanNoticeStore, query: LanePlanNoticeQuery): MutableList<LanePlanNotice> {
    return query.apply(store.list())
}
