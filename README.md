# setlist

🐦 Auto-generate Twitter lists based on your/others' following or list members.

[![Kotlin](https://img.shields.io/badge/Kotlin-1.4.30-blue)](https://kotlinlang.org)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/SlashNephy/setlist)](https://github.com/SlashNephy/setlist/releases)
[![GitHub Workflow Status](https://img.shields.io/github/workflow/status/SlashNephy/setlist/Docker)](https://hub.docker.com/r/slashnephy/setlist)
[![Docker Image Size (tag)](https://img.shields.io/docker/image-size/slashnephy/setlist/latest)](https://hub.docker.com/r/slashnephy/setlist)
[![Docker Pulls](https://img.shields.io/docker/pulls/slashnephy/setlist)](https://hub.docker.com/r/slashnephy/setlist)
[![license](https://img.shields.io/github/license/SlashNephy/setlist)](https://github.com/SlashNephy/setlist/blob/master/LICENSE)
[![issues](https://img.shields.io/github/issues/SlashNephy/setlist)](https://github.com/SlashNephy/setlist/issues)
[![pull requests](https://img.shields.io/github/issues-pr/SlashNephy/setlist)](https://github.com/SlashNephy/setlist/pulls)

## Requirements

- Java 8 or later

## Limitation

Due to hidden restrictions in the Twitter API, there is a limit to the number of users that can be added to or removed from a list within 24 hours.  
According to various articles on the Internet, this rate limit is said to be 300 users/24 hours.  
This means that editing members of the list may not be finished for some time.  
For more information, please see https://scrapbox.io/ci7lus/Twitter_lists%2Fmembers%2Fcreate_%E8%A6%8F%E5%88%B6.

## Get Started

### Docker

There are some image tags.

- `slashnephy/setlist:latest`  
  Automatically published every push to `master` branch.
- `slashnephy/setlist:dev`  
  Automatically published every push to `dev` branch.
- `slashnephy/setlist:<version>`  
  Coresponding to release tags on GitHub.

`docker-compose.yml`

```yaml
version: '3.8'

services:
  setlist:
    container_name: setlist
    image: slashnephy/setlist:latest
    restart: always
    environment:
      # Twitter の資格情報 (必須)
      TWITTER_CK: xxx
      TWITTER_CS: xxx
      TWITTER_AT: xxx
      TWITTER_ATS: xxx
      
      # マージ先のリスト情報
      # リスト ID かスラグ形式のいずれかで指定する
      # スラグは twitter.com でリストを表示した際の URL の末尾に対応する
      # リスト ID → スラグの順に優先される
      TARGET_LIST_ID: 100001
      TARGET_LIST_SLUG: list

      # マージ元の情報
      # ID で指定したリストのメンバーをマージ
      SOURCE_LIST_IDS: 20001,30001,40001
      # スラグで指定したリストのメンバーをマージ
      # {スクリーンネーム}/{スラグ} で指定する必要がある
      SOURCE_LIST_SLUGS: twitter/awesome_list,LoveLive_staff/nijigaku
      # ID で指定したユーザの following をマージ
      SOURCE_USER_IDS: 1000001,4000001
      # スクリーンネームで指定したユーザの following をマージ
      SOURCE_USER_SCREEN_NAMES: SlashNephy,UN_NERV
      # ユーザに自身を含めるか (デフォルト: 無効)
      SOURCE_USER_INCLUDE_SELF: 1

volumes:
  data:
    driver: local
```
