###############################################################################
# Copyright (c) 2017, 2018 Eurotech and/or its affiliates and others
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     Eurotech
###############################################################################
@tag
@env_none

Feature: Tag Service
  Tag Service is responsible for CRUD operations on Tags. This service is currently
  used to attach tags to Devices, but could be used to tag eny kapua entity, like
  User for example.

  Scenario: Creating tag
    Create a tag entry, with specified name. Name is only tag specific attribute.
    Once created search for it and is should been created.

    Given Tag with name "tagName"
    When Tag with name "tagName" is searched
    Then I find a tag with name "tagName"
      
  Scenario: Deleting tag
    Create a tag entry, with specified name. Name is only tag specific attribute.
    Once created search and find it, then delete it.

    Given Tag with name "tagName2"
    When Tag with name "tagName2" is searched
    Then I find and delete tag with name "tagName2"
